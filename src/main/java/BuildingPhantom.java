import model.Building;

import java.util.HashMap;

/**
 * Created by dragoon on 11/16/16.
 */
public class BuildingPhantom extends Building {

	private static final HashMap<Long, Long> ID_MAPPING = new HashMap<Long, Long>() {{
		put(20L, 22L);
		put(22L, 20L);

		put(19L, 21L);
		put(21L, 19L);

		put(17L, 23L);
		put(23L, 17L);

		put(24L, 18L);
		put(18L, 24L);

		put(13L, 14L);
		put(14L, 13L);

		put(15L, 16L);
		put(16L, 15L);

		put(11L, 12L);
		put(12L, 11L);
	}};

	private int life;
	private int remainingActionCooldownTicks;
	private boolean updated;
	private boolean broken;

	public BuildingPhantom(Building building, boolean enemy) {
		super(enemy ? ID_MAPPING.get(building.getId()) : building.getId(),
			  enemy ? Constants.getGame().getMapSize() - building.getX() : building.getX(),
			  enemy ? Constants.getGame().getMapSize() - building.getY() : building.getY(),
			  building.getSpeedX(),
			  building.getSpeedY(),
			  building.getAngle(),
			  enemy ? Constants.getEnemyFaction() : building.getFaction(),
			  building.getRadius(),
			  building.getLife(),
			  building.getMaxLife(),
			  building.getStatuses(),
			  building.getType(),
			  building.getVisionRange(),
			  building.getAttackRange(),
			  building.getDamage(),
			  building.getCooldownTicks(),
			  building.getRemainingActionCooldownTicks());
		this.life = building.getLife();
		this.remainingActionCooldownTicks = building.getRemainingActionCooldownTicks();
	}

	public void updateInfo(Building building) {
		this.life = building.getLife();
		this.remainingActionCooldownTicks = building.getRemainingActionCooldownTicks();
		updated = true;
	}

	public void resetUpdate() {
		updated = false;
	}

	public void nextTick() {
		this.remainingActionCooldownTicks = Math.max(0, this.remainingActionCooldownTicks - 1);
	}

	public boolean isUpdated() {
		return updated;
	}

	public boolean isBroken() {
		return broken;
	}

	public void setBroken(boolean broken) {
		this.broken = broken;
	}

	@Override
	public int getLife() {
		return life;
	}

	@Override
	public int getRemainingActionCooldownTicks() {
		return remainingActionCooldownTicks;
	}
}