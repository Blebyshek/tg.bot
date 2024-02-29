package com.example.bot.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserLikesRepository extends JpaRepository<UserLikes,Long> {

    @Query("SELECT ul FROM UserLikes ul WHERE ul.liked = :likedUser AND ul.liker = :likerUser")
    List<UserLikes> findByLikedAndLiker(User likedUser, User likerUser);


}
