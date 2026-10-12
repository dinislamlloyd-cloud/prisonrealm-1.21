package com.prisonrealm.prison;

import com.mojang.serialization.Codec;
import com.prisonrealm.PrisonRealmMod;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Сохраняемые данные: заключённые игроки (срок, где были, вещи)
 * и мобы, запечатанные в голубых кубах.
 */
public class PrisonState extends PersistentState {
	public static final Codec<PrisonState> CODEC =
			NbtCompound.CODEC.xmap(PrisonState::fromNbt, PrisonState::toNbt);

	public static final PersistentStateType<PrisonState> TYPE =
			new PersistentStateType<>(PrisonRealmMod.MOD_ID + "_prisoners", PrisonState::new, CODEC, null);

	public static class Entry {
		public int ticksLeft;
		public Identifier dimension = Identifier.of("minecraft", "overworld");
		public double x;
		public double y;
		public double z;
		public float yaw;
		public float pitch;
		public int gameMode;
		/** Вещи игрока: список {Slot, Item}. */
		public NbtList inventory = new NbtList();
		/** Голубой куб на месте запечатывания (может быть null). */
		public UUID cubeId;
	}

	/**
	 * Моб внутри голубого куба. Сам моб остаётся на месте, но невидим,
	 * неуязвим и неподвижен, пока куб не откроют.
	 */
	public static class SealedMob {
		public UUID cubeId;
		public UUID mobId;
		public Identifier dimension = Identifier.of("minecraft", "overworld");
		public double x;
		public double y;
		public double z;
		/** Высота центра куба. */
		public double centerY;
		public String name = "";
		// Каким моб был до запечатывания: это вернётся при открытии.
		public boolean wasInvisible;
		public boolean wasInvulnerable;
		public boolean wasNoGravity;
		public boolean wasSilent;
		public boolean wasNoAi;
	}

	private final Map<UUID, Entry> prisoners = new HashMap<>();
	private final List<SealedMob> mobs = new ArrayList<>();

	public static PrisonState get(MinecraftServer server) {
		return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE);
	}

	public boolean has(UUID id) {
		return prisoners.containsKey(id);
	}

	public Entry get(UUID id) {
		return prisoners.get(id);
	}

	public void put(UUID id, Entry entry) {
		prisoners.put(id, entry);
		markDirty();
	}

	public Entry remove(UUID id) {
		Entry removed = prisoners.remove(id);
		if (removed != null) {
			markDirty();
		}
		return removed;
	}

	public Map<UUID, Entry> all() {
		return prisoners;
	}

	/** Нет заключённых игроков (мобы в кубах не считаются). */
	public boolean isEmpty() {
		return prisoners.isEmpty();
	}

	public List<SealedMob> mobs() {
		return mobs;
	}

	public void addMob(SealedMob mob) {
		mobs.add(mob);
		markDirty();
	}

	public void removeMob(SealedMob mob) {
		if (mobs.remove(mob)) {
			markDirty();
		}
	}

	// ---------------------------------------------------------------- NBT

	private static void putUuid(NbtCompound c, String key, UUID id) {
		if (id != null) {
			c.putString(key, id.toString());
		}
	}

	private static UUID getUuid(NbtCompound c, String key) {
		String s = c.getString(key, "");
		if (s.isEmpty()) {
			return null;
		}
		try {
			return UUID.fromString(s);
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private static Identifier getId(NbtCompound c, String key) {
		Identifier id = Identifier.tryParse(c.getString(key, "minecraft:overworld"));
		return id != null ? id : Identifier.of("minecraft", "overworld");
	}

	public NbtCompound toNbt() {
		NbtCompound nbt = new NbtCompound();
		NbtList list = new NbtList();
		for (Map.Entry<UUID, Entry> me : prisoners.entrySet()) {
			Entry e = me.getValue();
			NbtCompound c = new NbtCompound();
			putUuid(c, "Id", me.getKey());
			c.putInt("Ticks", e.ticksLeft);
			c.putString("Dim", e.dimension.toString());
			c.putDouble("X", e.x);
			c.putDouble("Y", e.y);
			c.putDouble("Z", e.z);
			c.putFloat("Yaw", e.yaw);
			c.putFloat("Pitch", e.pitch);
			c.putInt("Mode", e.gameMode);
			c.put("Inventory", e.inventory);
			putUuid(c, "Cube", e.cubeId);
			list.add(c);
		}
		nbt.put("Prisoners", list);

		NbtList mobList = new NbtList();
		for (SealedMob m : mobs) {
			NbtCompound c = new NbtCompound();
			putUuid(c, "Cube", m.cubeId);
			putUuid(c, "Mob", m.mobId);
			c.putString("Dim", m.dimension.toString());
			c.putDouble("X", m.x);
			c.putDouble("Y", m.y);
			c.putDouble("Z", m.z);
			c.putDouble("CenterY", m.centerY);
			c.putString("Name", m.name);
			c.putBoolean("WasInvisible", m.wasInvisible);
			c.putBoolean("WasInvulnerable", m.wasInvulnerable);
			c.putBoolean("WasNoGravity", m.wasNoGravity);
			c.putBoolean("WasSilent", m.wasSilent);
			c.putBoolean("WasNoAi", m.wasNoAi);
			mobList.add(c);
		}
		nbt.put("Mobs", mobList);
		return nbt;
	}

	public static PrisonState fromNbt(NbtCompound nbt) {
		PrisonState state = new PrisonState();
		NbtList list = nbt.getListOrEmpty("Prisoners");
		for (int i = 0; i < list.size(); i++) {
			NbtCompound c = list.getCompoundOrEmpty(i);
			UUID id = getUuid(c, "Id");
			if (id == null) {
				continue;
			}
			Entry e = new Entry();
			e.ticksLeft = c.getInt("Ticks", 0);
			e.dimension = getId(c, "Dim");
			e.x = c.getDouble("X", 0.0);
			e.y = c.getDouble("Y", 0.0);
			e.z = c.getDouble("Z", 0.0);
			e.yaw = c.getFloat("Yaw", 0.0f);
			e.pitch = c.getFloat("Pitch", 0.0f);
			e.gameMode = c.getInt("Mode", 0);
			e.inventory = c.getListOrEmpty("Inventory");
			e.cubeId = getUuid(c, "Cube");
			state.prisoners.put(id, e);
		}

		NbtList mobList = nbt.getListOrEmpty("Mobs");
		for (int i = 0; i < mobList.size(); i++) {
			NbtCompound c = mobList.getCompoundOrEmpty(i);
			SealedMob m = new SealedMob();
			m.cubeId = getUuid(c, "Cube");
			m.mobId = getUuid(c, "Mob");
			m.dimension = getId(c, "Dim");
			m.x = c.getDouble("X", 0.0);
			m.y = c.getDouble("Y", 0.0);
			m.z = c.getDouble("Z", 0.0);
			m.centerY = c.getDouble("CenterY", 0.0);
			m.name = c.getString("Name", "");
			m.wasInvisible = c.getBoolean("WasInvisible", false);
			m.wasInvulnerable = c.getBoolean("WasInvulnerable", false);
			m.wasNoGravity = c.getBoolean("WasNoGravity", false);
			m.wasSilent = c.getBoolean("WasSilent", false);
			m.wasNoAi = c.getBoolean("WasNoAi", false);
			state.mobs.add(m);
		}
		return state;
	}
}
