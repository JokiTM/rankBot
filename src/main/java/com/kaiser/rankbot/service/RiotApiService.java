package com.kaiser.rankbot.service;


import com.kaiser.rankbot.responses.RankResponse;
import com.kaiser.rankbot.responses.SummonerPuuid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@Service
public class RiotApiService {

    private final RestTemplate restTemplate;
    private String apiKey;
    private static final Logger logger = LoggerFactory.getLogger(RiotApiService.class);


    public RiotApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.apiKey = System.getenv("RIOT_API_KEY");

    }



    public String getPuuid(String riotId) throws Exception {

        String name = riotId.substring(0 ,riotId.indexOf('#'));
        String tag = riotId.substring(riotId.indexOf('#')+1);



        String getPuuid = "https://europe.api.riotgames.com/riot/account/v1/accounts/by-riot-id/" + name + "/" + tag + "?api_key=" + apiKey;



            logger.info("Fetching puuid from riot API: {}", getPuuid);
            ResponseEntity<SummonerPuuid> response = restTemplate.getForEntity(getPuuid, SummonerPuuid.class);
            if(response.getStatusCode() != HttpStatus.OK || response.getBody().getPuuid() == null) {
                throw new Exception("Error while fetching puuid from riot API");
            }
            return response.getBody().getPuuid();

    }

    public RankResponse fetchRankFromRiotApi(String puuid) throws Exception {

        if(puuid == null) {
            throw new Exception("puuid is null");
        }

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
            logger.error("Fehler beim Abrufen des Ranges: {}", e.getMessage());
            return null;
        }
    }
}


