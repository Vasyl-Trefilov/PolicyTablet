package org.rusting.policytablet;

public class PlayerData {
    public static final String[] COUNTRIES = {"Russland", "USA", "Germany", "China"};

    public static String countryByIndex(int index) {
        return COUNTRIES[Math.abs(index) % COUNTRIES.length];
    }

    public static int indexOfCountry(String country) {
        for (int i = 0; i < COUNTRIES.length; i++) {
            if (COUNTRIES[i].equals(country)) return i;
        }
        return 0;
    }

    private int balance;
    private int income;
    private String country;

    public PlayerData(int balance, int income, String country) {
        this.balance = balance;
        this.income = income;
        this.country = country;
    }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public void addBalance(int amount) {
        this.balance += amount;
    }

    public int getIncome() {
        return income;
    }

    public void setIncome(int income) {
        this.income = income;
    }

    public void addIncome(int income) {
        this.income += income;
    }
}
