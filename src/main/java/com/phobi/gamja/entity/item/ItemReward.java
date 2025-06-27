package com.phobi.gamja.entity.item;

public class ItemReward {
    private Item item;
    private int count;

    public ItemReward(Item item, int count) {
        this.item = item;
        this.count = count;
    }

    public Item getItem() { return item; }
    public int getCount() { return count; }
}