package com.kaiser.rankbot.service;


import com.kaiser.rankbot.responses.RankResponse;
import com.kaiser.rankbot.responses.SummonerPuuid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

@Service
public class RiotApiService {

    private final RestTemplate restTemplate;
    private final String apiKey;
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
            if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                logger.warn("Rate limit reached for Riot API");
                throw new Exception("Rate limit reached");
            }
            
            var body = response.getBody();
            if (body == null) {
                logger.error("Received null response body from Riot API");
                throw new Exception("API returned null response");
            }
            
            for (RankResponse rankResponse : body) {
                if(rankResponse.getQueueType().equals("RANKED_SOLO_5x5"))
                    return rankResponse;
            }

            // User is unranked
            return null;

        } catch (Exception e) {
            logger.error("Error fetching rank data: Status={}, Message={}", 
                e instanceof HttpStatusCodeException ? ((HttpStatusCodeException) e).getStatusCode() : "Unknown",
                e.getMessage());
            
            if (e.getMessage().contains("429") || e.getMessage().contains("Too Many Requests")) {
                throw new Exception("Rate limit reached");
            }
            throw new Exception("API error: " + e.getMessage());
        }
    }
}