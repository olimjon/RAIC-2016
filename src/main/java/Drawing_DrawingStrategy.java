import model.ActionType;
import model.Building;
import model.CircularUnit;
import model.Game;
import model.LaneType;
import model.LivingUnit;
import model.Minion;
import model.MinionType;
import model.Move;
import model.Projectile;
import model.ProjectileType;
import model.Tree;
import model.Wizard;
import model.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by by.dragoon on 11/8/16.
 */
public class Drawing_DrawingStrategy extends StrategyImplement {

    private List<Drawing_DrawingData> drawingDataList;
    private Drawing_MainFrame mainFrame;
	private Drawing_DrawPanel drawPanel;
	private Drawing_TextInfoPanel textInfoPanel;
	private Game game;

    private boolean draw = false;

	private long timeSum = 0;

	private List<Integer> staffHits = new ArrayList<>();

    private static Drawing_DrawingStrategy instance;
	protected TreeMap<Double, ScanMatrixItem> foundScanMatrixItems = new TreeMap<>();

    public Drawing_DrawingStrategy() {
		drawingDataList = Collections.synchronizedList(new ArrayList<>());
		Drawing_DrawingStrategy.instance = this;
    }

    public void move(int tick) {
		synchronized (this) {
			Drawing_DrawingData drawingData = drawingDataList.get(tick);
			if (drawingData == null) {
				mainFrame.getDrawPanel().clear();
				mainFrame.getDrawPanel().addFigure(new Drawing_Line(0,
																	0,
																	Constants.getGame().getMapSize(),
																	Constants.getGame().getMapSize(),
																	Color.RED));
				mainFrame.getDrawPanel().addFigure(new Drawing_Line(0,
																	Constants.getGame().getMapSize(),
																	0,
																	Constants.getGame().getMapSize(),
																	Color.RED));
				mainFrame.getDrawPanel().repaint();
				return;
			}
			int lastTickMem = this.lastTick;
			while (tick > 0 && drawingDataList.get(--tick) == null) {
				// empty body is ok
			}
			this.lastTick = tick;
            LaneType laneTypeMem = this.myLine;
            this.myLine = drawingData.getMyLine();
			BuildingPhantom[] mem = this.BUILDING_PHANTOMS;
			this.BUILDING_PHANTOMS = drawingData.getBuildingPhantoms();
			move(drawingData.getSelf(), drawingData.getWorld(), game, new Move(), true);
			this.BUILDING_PHANTOMS = mem;
			this.myLine = laneTypeMem;
            this.lastTick = lastTickMem;
		}
	}

    @Override
    public void move(Wizard self, World world, Game game, Move move) {
		synchronized (this) {
			if (mainFrame == null) {
				mainFrame = new Drawing_MainFrame(world.getWidth(), world.getHeight());
				drawPanel = mainFrame.getDrawPanel();
				textInfoPanel = mainFrame.getTextInfoPanel();
				this.game = game;
			}
			while (drawingDataList.size() < world.getTickIndex()) {
				drawingDataList.add(null);
			}
			drawingDataList.add(new Drawing_DrawingData(self, world, this.myLine, this.BUILDING_PHANTOMS));
			mainFrame.getSlider().setMaximum(Math.max(world.getTickIndex(), mainFrame.getSlider().getMaximum()));
			move(self, world, game, move, false);
		}
    }

	private void drawUnit(LivingUnit unit) {
		drawUnit(unit, null, null);
	}

	private void drawUnit(LivingUnit unit, Color color) {
		drawUnit(unit, null, color);
	}

	private void drawUnit(LivingUnit unit, Double visibleDistance, Color color) {
		if (color == null) {
			color = Constants.getCurrentFaction() == unit.getFaction() ?
					Color.green :
					Constants.getEnemyFaction() == unit.getFaction() ?
							Color.red :
							Color.blue;
		}
		drawPanel.addFigure(new Drawing_Circle(unit.getX(),
											   unit.getY(),
											   unit.getRadius(),
											   true,
											   color));

		if (unit instanceof Minion && ((Minion) unit).getType() == MinionType.FETISH_BLOWDART) {
			double angle = unit.getAngle();
			angle += Math.PI / 2.;
			drawPanel.addFigure(new Drawing_Line(unit.getX() + Math.cos(angle) * unit.getRadius(),
												 unit.getY() + Math.sin(angle) * unit.getRadius(),
												 unit.getX() - Math.cos(angle) * unit.getRadius(),
												 unit.getY() - Math.sin(angle) * unit.getRadius(),
												 Color.black));
		}

		drawPanel.addFigure(new Drawing_Circle(unit.getX(),
											   unit.getY(),
											   2.,
											   true,
											   Color.yellow));

		drawPanel.addFigure(new Drawing_Line(unit.getX(),
											 unit.getY(),
											 unit.getX() + (unit.getRadius() + 10.) * Math.cos(unit.getAngle()),
											 unit.getY() + (unit.getRadius() + 10.) * Math.sin(unit.getAngle()),
											 Color.black));

		double[] polygonX = new double[4];
		polygonX[0] = polygonX[1] = unit.getX() - unit.getRadius();
		polygonX[2] = polygonX[3] = unit.getX() + unit.getRadius();
		double[] polygonY = new double[4];
		double wide = unit.getRadius() / 10.;
		polygonY[0] = polygonY[3] = unit.getY() - unit.getRadius() - wide;
		polygonY[1] = polygonY[2] = unit.getY() - unit.getRadius() - wide * 2.;

		drawPanel.addFigure(new Drawing_Polygon(polygonX, polygonY, true, Color.black));

		polygonX = Arrays.copyOf(polygonX, polygonX.length);
		polygonX[2] = polygonX[3] = polygonX[0] + (unit.getLife() / (double) unit.getMaxLife()) * unit.getRadius() * 2.;
		drawPanel.addFigure(new Drawing_Polygon(polygonX, polygonY, true, Color.RED));

		if (visibleDistance != null) {
			drawPanel.addFigure(new Drawing_Circle(unit.getX(),
												   unit.getY(),
												   visibleDistance,
												   Color.YELLOW));
		}
	}

    private void move(Wizard self, World world, Game game, Move move, boolean draw) {
        this.draw = draw;
        if (draw) {
            drawData(drawingDataList.get(world.getTickIndex()));
            TopLine topLine = Constants.getTopLine();
            mainFrame.getDrawPanel().addFigure(new Drawing_Line(0,
                                                                topLine.getLineDistance(),
                                                                game.getMapSize(),
                                                                topLine.getLineDistance(),
                                                                Color.RED));

            mainFrame.getDrawPanel().addFigure(new Drawing_Line(topLine.getLineDistance(),
                                                                0,
                                                                topLine.getLineDistance(),
                                                                game.getMapSize(),
                                                                Color.RED));

            mainFrame.getDrawPanel().addFigure(new Drawing_Line(0,
                                                                topLine.getLineDistance() * 5.,
                                                                topLine.getLineDistance() * 5.,
                                                                0,
                                                                Color.RED));

            mainFrame.getDrawPanel().addFigure(new Drawing_Line(game.getMapSize(),
                                                                0,
                                                                0,
                                                                game.getMapSize(),
                                                                Color.RED));
            BottomLine bottomLine = Constants.getBottomLine();
            mainFrame.getDrawPanel().addFigure(new Drawing_Line(0,
                                                                bottomLine.getLineDistance(),
                                                                game.getMapSize(),
                                                                bottomLine.getLineDistance(),
                                                                Color.RED));
            mainFrame.getDrawPanel().addFigure(new Drawing_Line(bottomLine.getLineDistance(),
                                                                0,
                                                                bottomLine.getLineDistance(),
                                                                game.getMapSize(),
                                                                Color.RED));

            mainFrame.getDrawPanel().addFigure(new Drawing_Line(0,
                                                                bottomLine.getCornerCompare(),
                                                                bottomLine.getCornerCompare(),
                                                                0,
                                                                Color.RED));
        }
		moveToPoint = null;
		long time = System.nanoTime();
		super.move(self, world, game, move);
		time = System.nanoTime() - time;
		System.out.println("Call took " + nanosToMsec(time) + "ms");
		if (!draw) {
			timeSum += time;
			System.out.println("Total took " + nanosToMsec(timeSum) + " nanos on " + world.getTickIndex() + " ticks");
		}

		if (draw) {
			drawUpdatedData(self);
		}
	}

	private void drawUpdatedData(Wizard self) {
		for (BuildingPhantom buildingPhantom : BUILDING_PHANTOMS) {
			if (buildingPhantom.isUpdated()) {
				drawUnit(buildingPhantom);
			} else {
				drawUnit(buildingPhantom, Color.pink);
			}
		}

		double maxScore = Double.MIN_VALUE;
		double minScore = Double.MAX_VALUE;
		for (int i = 0; i != scan_matrix.length; ++i) {
			for (int j = 0; j != scan_matrix[0].length; ++j) {
				if (!scan_matrix[i][j].isAvailable()) {
					continue;
				}
				double score = scan_matrix[i][j].getTotalScore(self);
				if (minScore > score) {
					minScore = score;
				}
				if (maxScore < score) {
					maxScore = score;
				}
			}
		}
		for (int i = 0; i != scan_matrix.length; ++i) {
			for (int j = 0; j != scan_matrix[0].length; ++j) {
				ScanMatrixItem item = scan_matrix[i][j];
				if (!item.isAvailable()) {
					continue;
				}

				drawPanel.addFigure(new Drawing_Circle(item.getX(),
													   item.getY(),
													   getColor(item.getTotalScore(self), minScore, maxScore)));
			}
		}

		for (ScanMatrixItem scanMatrixItem : foundScanMatrixItems.values()) {
			drawCross(scanMatrixItem, 5., Color.MAGENTA);
		}

		Point filterPoint = new Point(self.getX() + Math.cos(direction) * Constants.MOVE_SCAN_FIGURE_CENTER,
									  self.getY() + Math.sin(direction) * Constants.MOVE_SCAN_FIGURE_CENTER);
		drawPanel.addFigure(new Drawing_Circle(filterPoint.getX(), filterPoint.getY(), Constants.MOVE_DISTANCE_FILTER, Color.red));
		drawPanel.addFigure(new Drawing_Circle(filterPoint.getX(), filterPoint.getY(), Constants.getFightDistanceFilter(), Color.red));
		if (pointToReach != null) {
			drawCross(pointToReach, 5., Color.black);
		}
		if (moveToPoint != null) {
			drawCross(moveToPoint, 5., Color.blue);
		}
		if (!wayPoints.isEmpty()) {
			Iterator<WayPoint> iterator = wayPoints.iterator();
			WayPoint curr = null, prev;
			if (iterator.hasNext()) {
				curr = iterator.next();
			}
			while (iterator.hasNext()) {
				prev = curr;
				curr = iterator.next();
				drawLine(drawPanel, prev.getPoint(), curr.getPoint(), Color.BLUE);
			}
			drawLine(drawPanel, wayPoints.get(0).getPoint(), moveToPoint, Color.orange);
		}

		if (target != null) {
			drawCross(new Point(target.getX(), target.getY()), target.getRadius(), Color.BLACK);
			if (meleeTarget != null && meleeTarget != target) {
				drawCross(new Point(meleeTarget.getX(), meleeTarget.getY()), meleeTarget.getRadius(), Color.BLACK);
			}
		}

		StringBuilder sb = new StringBuilder("Action timeout: " + self.getRemainingActionCooldownTicks());
		sb.append(" [");
		for (ActionType actionType : ActionType.values()) {
			sb.append(", ").append(actionType.toString()).append(": ").append(self.getRemainingCooldownTicksByAction()[actionType.ordinal()]);
		}
		sb.append("]");
		textInfoPanel.putText(sb.toString(), 1);
		textInfoPanel.putText(String.format("hp:%d/%d", self.getLife(), self.getMaxLife()), 2);

		sb = new StringBuilder("Staff hits ticks: ");
		if (staffHits.isEmpty()) {
			sb.append("none");
		} else {
			for (Integer staffHit : staffHits) {
				sb.append(staffHit).append(" ");
			}
		}
		textInfoPanel.putText(sb.toString(), 3);
		textInfoPanel.putText(String.format("SpeedX: %s, SpeedY: %s, speed: %s",
											self.getSpeedX(),
											self.getSpeedY(),
											FastMath.hypot(self.getSpeedX(), self.getSpeedY())),
							  4);
	}

	private void drawLine(Drawing_DrawPanel drawPanel, Point pointA, Point pointB, Color color) {
		drawPanel.addFigure(new Drawing_Line(pointA.getX(),
											 pointA.getY(),
											 pointB.getX(),
											 pointB.getY(),
											 color));

	}

	private String nanosToMsec(long nano) {
		return String.valueOf(nano / 1e6);
	}

	private void drawCross(Point point, double size, Color color) {
		drawPanel.addFigure(new Drawing_Line(point.getX() - size,
											 point.getY() - size,
											 point.getX() + size,
											 point.getY() + size,
											 color));
		drawPanel.addFigure(new Drawing_Line(point.getX() - size,
											 point.getY() + size,
											 point.getX() + size,
											 point.getY() - size,
											 color));
	}

	@Override
	protected boolean checkHit(double angle, CircularUnit target, Move move) {
		boolean value = super.checkHit(angle, target, move);
		if (value && !draw) {
			staffHits.add(world.getTickIndex());
		}
		return value;
	}

	@Override
	protected void getBestMovePoint() {
		super.getBestMovePoint();
		foundScanMatrixItems.clear();
		for (int i = 0; i != scan_matrix.length; ++i) {
			for (int j = 0; j != scan_matrix[0].length; ++j) {
				ScanMatrixItem newScanMatrixItem = scan_matrix[i][j];
				if (newScanMatrixItem.getWayPoint() == null) {
					continue;
				}
				double key = newScanMatrixItem.getTotalScore(self);
				Map.Entry<Double, ScanMatrixItem> doubleScanMatrixItemEntry = foundScanMatrixItems.floorEntry(key);
				while (doubleScanMatrixItemEntry != null &&
						doubleScanMatrixItemEntry.getValue().getWayPoint().getDangerOnWay() >= newScanMatrixItem.getWayPoint().getDangerOnWay()) {
					foundScanMatrixItems.remove(doubleScanMatrixItemEntry.getKey());
					doubleScanMatrixItemEntry = foundScanMatrixItems.floorEntry(key);
				}
				doubleScanMatrixItemEntry = foundScanMatrixItems.ceilingEntry(key);
				if (doubleScanMatrixItemEntry == null ||
						doubleScanMatrixItemEntry.getValue().getWayPoint().getDangerOnWay() > newScanMatrixItem.getWayPoint().getDangerOnWay()) {
					foundScanMatrixItems.put(key, newScanMatrixItem);
				}
			}
		}
	}

	private void drawData(Drawing_DrawingData drawingData) {
		Wizard self = drawingData.getSelf();
		World world = drawingData.getWorld();

        Drawing_DrawPanel drawPanel = mainFrame.getDrawPanel();
        drawPanel.clear();
		// paint FOW
		double[] x = new double[]{0, 0, Constants.getGame().getMapSize(), Constants.getGame().getMapSize()};
		double[] y = new double[]{0, Constants.getGame().getMapSize(), Constants.getGame().getMapSize(), 0};
		drawPanel.addFigure(new Drawing_Polygon(x, y, true, Color.gray, -1000));
		for (Building unit : world.getBuildings()) {
			if (unit.getFaction() != Constants.getCurrentFaction()) {
				continue;
			}
			drawPanel.addFigure(new Drawing_Circle(unit.getX(), unit.getY(), unit.getVisionRange(), true, Color.white, -1000));
		}
		for (Wizard unit : world.getWizards()) {
			if (unit.getFaction() != Constants.getCurrentFaction()) {
				continue;
			}
			drawPanel.addFigure(new Drawing_Circle(unit.getX(), unit.getY(), unit.getVisionRange(), true, Color.white, -1000));
		}
		for (Minion unit : world.getMinions()) {
			if (unit.getFaction() != Constants.getCurrentFaction()) {
				continue;
			}
			drawPanel.addFigure(new Drawing_Circle(unit.getX(), unit.getY(), unit.getVisionRange(), true, Color.white, -1000));
		}

        Drawing_TextInfoPanel textPanel = mainFrame.getTextInfoPanel();
        textPanel.clear();

        textPanel.putText(String.format("Tick %d", world.getTickIndex()), 0);

        for (Tree unit : world.getTrees()) {
            Color borderColor = Color.black;
            switch (Utils.whichLine(unit)) {
                case 0:
                    borderColor = Color.black;
                    break;
                case 1:
                    borderColor = Color.red;
                    break;
                case 2:
                    borderColor = Color.darkGray;
                    break;
            }
            drawPanel.addFigure(new Drawing_Circle(unit.getX(),
                                                   unit.getY(),
                                                   unit.getRadius(),
                                                   true,
                                                   getHpColor(unit.getLife(), unit.getMaxLife()),
                                                   borderColor));
        }

        for (Wizard unit : world.getWizards()) {
			drawUnit(unit);//, unit.getVisionRange()
		}

        for (Minion unit : world.getMinions()) {
			drawUnit(unit);//, unit.getVisionRange()
		}

        for (Projectile unit : world.getProjectiles()) {
            drawPanel.addFigure(new Drawing_Circle(unit.getX(),
                                                   unit.getY(),
                                                   unit.getRadius(),
                                                   true,
                                                   getProjectiveColor(unit.getType())));
        }
		drawPanel.addFigure(new Drawing_Circle(self.getX(),
											   self.getY(),
											   self.getCastRange(),
											   false,
											   Color.blue));

		mainFrame.repaint();
	}

    private Color getHpColor(int currLife, int maxLife) {
        if (currLife == maxLife) {
            return Color.green;
        }
        if (currLife == 1) {
            return Color.red;
        }
        double center = (maxLife - 1) / 2.;
        --currLife;

        int red = 255;
        int green = 255;
        if (currLife >= center) {
            red *= (maxLife - 1 - currLife) / center;
        } else {
            green *= currLife / center;
        }
        return new Color(red, green, 0);
    }

	private Color getHpColor(double currValue, double maxValue) {
		if (currValue <= 0.) {
			return Color.red;
		}
		if (currValue > maxValue * 0.999) {
			return Color.green;
		}
		double center = maxValue / 2.;

		int red = 255;
		int green = 255;
		if (currValue >= center) {
			red *= (maxValue - currValue) / center;
		} else {
			green *= currValue / center;
		}
		return new Color(red, green, 0);
	}

	private Color getColor(double value, double minValue, double maxValue) {
		return getHpColor(value - minValue, maxValue - minValue);
	}

    private Color getProjectiveColor(ProjectileType type) {
        switch (type) {
            case MAGIC_MISSILE:
                return Color.MAGENTA;
            case FROST_BOLT:
                return Color.blue;
            case FIREBALL:
                return Color.ORANGE;
            case DART:
                return Color.DARK_GRAY;
        }
        return Color.black;
    }

    public static Drawing_DrawingStrategy getInstance() {
        return instance;
    }
}
