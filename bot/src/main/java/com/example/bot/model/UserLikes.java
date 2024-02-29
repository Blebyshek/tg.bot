package com.example.bot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class UserLikes {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "likerId")
    private User liker;

    @ManyToOne
    @JoinColumn(name = "likedId")
    private User liked;

    @Column(name = "booleanLiker")
    private boolean booleanLiker;
    @Column (name = "booleanLiked")
    private boolean booleanLiked;




}
