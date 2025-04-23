package com.kaiser.rankbot.service;


import com.kaiser.rankbot.responses.RankResponse;
import com.kaiser.rankbot.responses.SummonerPuuid;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@Service
public class RiotApiService {

    private final RestTemplate restTemplate;
    private String apiKey;

    public RiotApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.apiKey = System.getenv("RIOT_API_KEY");

    }



    public String getPuuid(String riotId) {

        String name = riotId.substring(0 ,riotId.indexOf('#'));
        String tag = riotId.substring(riotId.indexOf('#')+1);



        String getPuuid = "https://europe.api.riotgames.com/riot/account/v1/accounts/by-riot-id/" + name + "/" + tag + "?api_key=" + apiKey;

        try {
            ResponseEntity<SummonerPuuid> response = restTemplate.getForEntity(getPuuid, SummonerPuuid.class);
            return response.getBody().getPuuid();
        } catch (Exception e) {
            System.err.println("Fehler beim Abrufen der Summoner-ID: " + e.getMessage());
            return null;
        }
    }

    public RankResponse fetchRankFromRiotApi(String puuid) {


        String url = "https://euw1.api.riotgames.com/lol/league/v4/entries/by-puuid/" + puuid + "?api_key=" + apiKey;
        try {
            ResponseEntity<RankResponse[]> response = restTemplate.getForEntity(url, RankResponse[].class);
            var body =response.getBody();
            for (RankResponse rankResponse : body) {
                if(rankResponse.getQueueType().equals("RANKED_SOLO_5x5"))
                    return rankResponse;
            }

            return null;

        } catch (Exception e) {
            System.err.println("Fehler beim Abrufen des Ranges: " + e.getMessage());
            return null;
        }
    }
}


