package com.kaiser.rankbot.responses;

import lombok.Data;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class RankResponse {

    private String leagueId;
    private String queueType;
    private String tier;
    private String rank;
    private String summonerId;
    private String puuid;
    private Integer leaguePoints;
    private Integer wins;
    private Integer losses;
    private Boolean veteran;
    private Boolean inactive;
    private Boolean freshBlood;
    private Boolean hotStreak;
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();


    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}