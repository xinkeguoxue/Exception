package exception;

import java.util.Random;

import battlecode.common.*;

public class RobotPlayer {
	static RobotController rc;
	static Team myTeam;
	static Team enemyTeam;
	static int myRange;
	static Direction facing;
	static Random rand;

	
	public static void run(RobotController poop) {
		rc = poop;
		myRange = rc.getType().attackRadiusSquared;
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		rand = new Random(rc.getID());
		facing = getRandomDirection();//randomizes spawn location
		
		
		while (true) {
			try { 
				//spawns a BEAVER
				if(rc.getType() == RobotType.HQ) {
					attackEnemyZero();
					spawnUnit(RobotType.BEAVER);
					

					}
				else if (rc.getType() == RobotType.BEAVER){
					attackEnemyZero();
					if(Clock.getRoundNum() <= 500) {
						buildBuilding(RobotType.MINERFACTORY);
					}else if(Clock.getRoundNum() > 500){
						buildBuilding(RobotType.BARRACKS);
					}
					mineAndMove();
				}else if (rc.getType() == RobotType.MINER){
					attackEnemyZero();
					mineAndMove();
				}else if (rc.getType() == RobotType.MINERFACTORY){
					spawnUnit(RobotType.MINER);
				}else if (rc.getType() == RobotType.BARRACKS){
					spawnUnit(RobotType.SOLDIER);
				}else if (rc.getType() == RobotType.TOWER) {
					attackEnemyZero();
				}else if (rc.getType() == RobotType.SOLDIER){
					attackEnemyZero();
					moveAround();
				}
				transferSupplies();
				
			}
			catch (
			GameActionException e) {
				e.printStackTrace();
			}
			
		
			
//			//make HQ and Towers attack
//			if (rc.getType() == RobotType.HQ) {
//				try {
//					if (rc.isWeaponReady()) {
//						attackSomething();
//					}
//				} catch (Exception e) {
//					System.out.println("HQ Exception");
//					e.printStackTrace();
//				}
//			}
//			
//			 if (rc.getType() == RobotType.TOWER) {
//				try {
//					if (rc.isWeaponReady()) {
//						attackSomething();
//					}
//				} catch (Exception e) {
//					System.out.println("Tower Exception");
//					e.printStackTrace();
//				}
//			}
			rc.yield();
		}
	}
	
	private static void transferSupplies() throws GameActionException {
		RobotInfo[] nearbyAllies = rc.senseNearbyRobots(rc.getLocation(),GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, rc.getTeam());
		double lowestSupply = rc.getSupplyLevel();
		double transferAmount = 0;
		MapLocation suppliesToThisLocation = null;
		for(RobotInfo ri: nearbyAllies){
			if(ri.supplyLevel<lowestSupply){
				lowestSupply = ri.supplyLevel;
				transferAmount = (rc.getSupplyLevel()-ri.supplyLevel)/2;
				suppliesToThisLocation = ri.location;
			}
		}
		if(suppliesToThisLocation!=null){
			rc.transferSupplies((int)transferAmount, suppliesToThisLocation);
		}
	}
	
	private static void buildBuilding(RobotType type) throws GameActionException {
		if(rc.getTeamOre()>type.oreCost){
			Direction buildDir = getRandomDirection();
			if(rc.isCoreReady() && rc.canBuild(buildDir, type)){
				rc.build(buildDir, type);
			}
		}
	}
	private static void attackEnemyZero() throws GameActionException {
		RobotInfo[] nearbyEnemies = rc.senseNearbyRobots( rc.getType().attackRadiusSquared,rc.getTeam().opponent());
		if(nearbyEnemies.length>0){//there are enemy units
			//try to shoot at them
			//specifically, try to shoot at enemies specified by nearbyEnemies[0]
			if(rc.isWeaponReady() && rc.canAttackLocation(nearbyEnemies[0].location)){
				rc.attackLocation(nearbyEnemies[0].location);
			}
		}
	}
	
	private static void spawnUnit(RobotType type) throws GameActionException {
		Direction randomDir = getRandomDirection();
		if(rc.isCoreReady() && rc.canSpawn(randomDir, type)) {
			rc.spawn(randomDir, type);
		}
	}

	private static Direction getRandomDirection() {
	
		return Direction.values()[(int)(rand.nextDouble()*8)];
	}

	private static void mineAndMove() throws GameActionException {
		if(rc.senseOre(rc.getLocation())>1) {//if there is ore, try to mine
			if(rc.isCoreReady() && rc.canMine()) {
				rc.mine();
			}
		}
		moveAround();
	}

	private static void moveAround() throws GameActionException {
		if(rand.nextDouble()<.1){
			moveLeftRight();
			}
		MapLocation tileInFront = rc.getLocation().add(facing);
		
		//check that the tile in front is not a tile that can be attacked by an enemy tower
		MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
		boolean tileInFrontSafe = true;
		for(MapLocation m: enemyTowers){
			if(m.distanceSquaredTo(tileInFront) <=RobotType.TOWER.attackRadiusSquared){
				tileInFrontSafe = false;
				break;
			}
		}
		//check that we are not facing off the edge of the map
		if(rc.senseTerrainTile(tileInFront) != TerrainTile.NORMAL||!tileInFrontSafe) {
			moveLeftRight();
		}else{
			while (rc.canMove(facing) == false){
				moveLeftRight();
			}
			if(rc.isCoreReady() && rc.canMove(facing)){
				rc.move(facing);
			}
		}
//		if(rc.isCoreReady() && rc.canMove(facing)){
//			rc.move(facing);
//		}
	}
	

	private static void moveLeftRight() {
		if(rand.nextDouble()<.5){
			facing=facing.rotateLeft();
		}else {
			facing=facing.rotateRight();
		}
	}
	static void attackSomething() throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(myRange, enemyTeam);
		if (enemies.length > 0) {
			rc.attackLocation(enemies[0].location);
		}
		
	}
	
}
