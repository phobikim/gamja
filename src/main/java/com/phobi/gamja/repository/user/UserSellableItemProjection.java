package com.phobi.gamja.repository.user;

public interface UserSellableItemProjection {
    Long getItemId();
    String getName();
    String getDescription();
    String getIconPath();
    int getRank();
    int getQuantity();
    Integer getSellPrice();
}