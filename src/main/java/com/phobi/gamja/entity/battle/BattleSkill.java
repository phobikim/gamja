package com.phobi.gamja.entity.battle;

public class BattleSkill {
    public enum Type {
        BASIC,
        SPECIAL,
        UNIQUE
    }

    public enum Effect {
        DAMAGE,
        HEAL,
        BUFF
    }

    public enum Target {
        SELF,
        ENEMY
    }
}
