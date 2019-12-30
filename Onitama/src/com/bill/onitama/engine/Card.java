package com.bill.onitama.engine;

public enum Card {
	BOAR( new Manuever [] { new Manuever(-1, 0), new Manuever(1, 0), new Manuever(0, -1)}, "images/Boar.jpg"),
	COBRA( new Manuever [] {new Manuever(-1, 0), new Manuever(1, 1), new Manuever(1, -1)}, "images/Cobra.jpg"),
	CRAB( new Manuever [] {new Manuever(-2, 0), new Manuever(2, 0), new Manuever(0, -1)}, "images/Crab.jpg"),
	CRANE( new Manuever [] {new Manuever(-1, 1), new Manuever(1, 1), new Manuever(0, -1)}, "images/Crane.jpg"),
	DRAGON( new Manuever [] {new Manuever(-1, 1), new Manuever(1, 1), new Manuever(2, -1), new Manuever(-2, -1)}, "images/Dragon.jpg"),
	EEL( new Manuever [] {new Manuever(-1, 1), new Manuever(1, 0), new Manuever(-1, -1)}, "images/Eel.jpg"),
	ELEPHANT( new Manuever [] {new Manuever(-1, 0), new Manuever(1, 0), new Manuever(1, -1), new Manuever(-1, -1)}, "images/Elephant.jpg"),
	FROG( new Manuever [] {new Manuever(-2, 0), new Manuever(-1, -1), new Manuever(1, 1)}, "images/Frog.jpg"),
	GOOSE( new Manuever [] {new Manuever(-1, 0), new Manuever(1, 0), new Manuever(1, 1), new Manuever(-1, -1)}, "images/Goose.jpg"),
	HORSE( new Manuever [] {new Manuever(-1, 0), new Manuever(0, -1), new Manuever(0, 1)}, "images/Horse.jpg"),
	MANTIS( new Manuever [] {new Manuever(0, 1), new Manuever(-1, -1), new Manuever(1, -1)}, "images/Mantis.jpg"),
	MONKEY( new Manuever [] {new Manuever(-1, 1), new Manuever(1, 1), new Manuever(1, -1), new Manuever(-1, -1)}, "images/Monkey.jpg"),
	OX( new Manuever [] {new Manuever(1, 0), new Manuever(0, 1), new Manuever(0, -1)}, "images/Ox.jpg"),
	RABBIT( new Manuever [] {new Manuever(-1, 1), new Manuever(1, -1), new Manuever(2, 0)}, "images/Rabbit.jpg"),
	ROOSTER( new Manuever [] {new Manuever(-1, 0), new Manuever(1, 0), new Manuever(1, -1), new Manuever(-1, 1)}, "images/Rooster.jpg"),
	TIGER( new Manuever [] {new Manuever(0, 1), new Manuever(0, -2)}, "images/Tiger.jpg");
	
	private Manuever [] manuevers;
	private String imageFileName;
	
	private Card(Manuever [] manuevers, String imageFileName){
		this.manuevers = manuevers;
		this.imageFileName = imageFileName;
	}
	
	public Manuever [] getManuevers(){
		return manuevers;
	}
	
	public String getImageFileName(){
		return imageFileName;
	}
}
