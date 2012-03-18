package hkust.comp3111h.ballcraft.client;

import hkust.comp3111h.ballcraft.graphics.Drawable;
import hkust.comp3111h.ballcraft.server.Unit;

import java.util.ArrayList;

public class ClientGameState {
	
	private static ClientGameState stateInstance;
	
	private ArrayList<Drawable> drawables;
	private ArrayList<Skill> skills;
	
	private ClientGameState() {
		drawables = new ArrayList<Drawable>();
		skills = new ArrayList<Skill>();
	}
	
	public static ClientGameState getClientGameState() {
		if (stateInstance == null) {
			stateInstance = new ClientGameState();
		}
		return stateInstance;
	}
	
	public void applyUpdater(GameUpdater updater) {
		ArrayList<Unit> units = updater.getUnits();
		for (int i = 0; i < units.size(); i++) {
			if (i < drawables.size()) {
				drawables.set(i, units.get(i));
			} else {
				drawables.add(units.get(i));
			}
		}
	}
	
	public void addDrawable(Drawable drawable) {
		drawables.add(drawable);
	}
	
	public void addSkill(Skill skill) {
		skills.add(skill);
	}
	
	public ArrayList<Drawable> getDrawables() {
		return drawables;
	}
	
	public ArrayList<Skill> getSkills() {
		return skills;
	}

}