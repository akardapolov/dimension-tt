package ru.dimension.tt.data;

public enum Department {
  ENGINEERING("Engineering", "⚙️"),
  SALES("Sales", "💰"),
  MARKETING("Marketing", "📢"),
  HR("Human Resources", "👥"),
  FINANCE("Finance", "📊");

  private final String displayName;
  private final String emoji;

  Department(String displayName, String emoji) {
    this.displayName = displayName;
    this.emoji = emoji;
  }

  public String displayName() {
    return displayName;
  }

  public String emoji() {
    return emoji;
  }

  @Override
  public String toString() {
    return displayName;
  }
}