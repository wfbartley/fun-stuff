package com.wbartley.rushhour;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public enum Vehicle {
	RED_CAR(2, new Color(162, 21, 30), "X"),
	LIME_CAR(2, new Color(111, 173, 134), "A"),
	ORANGE_CAR(2, new Color(221, 126, 128), "B"),
	CYAN_CAR(2, new Color(74, 139, 145), "C"),
	PINK_CAR(2, new Color(221, 149, 134), "D"),
	VIOLET_CAR(2, new Color(120, 80, 130), "E"),
	GREEN_CAR(2, new Color(1, 97, 86), "F"),
	BLACK_CAR(2, Color.BLACK, "G"),
	TAN_CAR(2, new Color(198, 165, 134), "H"),
	YELLOW_CAR(2, new Color(212, 189, 113), "I"),
	BROWN_CAR(2, new Color(165, 42, 42), "J"),
	KHAKI_CAR(2, new Color(82, 90, 49), "K"),
	YELLOW_TRUCK(3, new Color(247, 186, 59), "O"),
	PURPLE_TRUCK(3, new Color(181, 143, 156), "P"),
	BLUE_TRUCK(3, new Color(60, 118, 182), "Q"),
	TURQUOISE_TRUCK(3, new Color(50, 172, 196), "R");
	
	public static int carSize = 2;
	public static int truckSize = 3;
	private int length;
	private Color color;
	private String nickname;
	private static Map<String, Vehicle> nicknameMap = new HashMap<String, Vehicle>();
	private static int firstTruckIdx = 0;
	static {
		int idx = 0;
		for (Vehicle vehicle : Vehicle.values()) {
			nicknameMap.put(vehicle.nickname, vehicle);
			if (vehicle.length == truckSize && firstTruckIdx == 0) {
				firstTruckIdx = idx;
			}
			idx++;
		}
	}
	
	private Vehicle(int length, Color color, String nickname) {
		this.length = length;
		this.color = color;
		this.nickname = nickname;
	}
	
	public int getLength() {
		return length;
	}
	
	public Color getColor() {
		return color;
	}
	
	public String getNickname() {
		return nickname;
	}
	
	
	public static Vehicle fromNickname(String nickname) {
		return nicknameMap.get(nickname);
	}
	
	public static int getMaxCars() {
		return firstTruckIdx-1;
	}
	
	public static int getMaxTrucks() {
		return values().length - firstTruckIdx;
	}	
}
