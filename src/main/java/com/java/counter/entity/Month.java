package com.java.counter.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "month", schema = "main")
@Getter
@Setter
public class Month {
    @Id
    @GeneratedValue
    private UUID id;

    private Integer year;
    private Integer month;
    private Integer count;

    @OneToMany(mappedBy = "month", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DateCount> dataCount = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}
