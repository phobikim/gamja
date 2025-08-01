package com.phobi.gamja.entity.user;

public enum CounterType {
    NONE,
    MONSTER_KILL,
    ITEM_CRAFT,
    CHARACTER_DRAW,
    LIFE_ACTION,
    QUEST_COMPLETE,
    ITEM_ENHANCE,
    ITEM_ALCHEMY,

    /*기록 안함, 퀘스트 용 */
    EQUIP_ITEM,
    EQUIP_TITLE,
    DELIVER_ITEM
}