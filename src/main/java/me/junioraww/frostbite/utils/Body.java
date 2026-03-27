package me.junioraww.frostbite.utils;

public class Body {
  private int temperature = 0;

  public int getTemperature() {
    return temperature;
  }

  public void setTemperature(int temperature) {
    this.temperature = temperature;
  }

  public void add(int amount) {
    this.temperature += amount;
  }

  public void sub(int amount) {
    this.temperature -= amount;
  }
}
