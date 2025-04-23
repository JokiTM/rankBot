package com.kaiser.rankbot.repo;

import com.kaiser.rankbot.UserRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RankRepo extends JpaRepository<UserRank, Integer>{

}
