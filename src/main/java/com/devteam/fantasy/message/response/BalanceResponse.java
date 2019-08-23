package com.devteam.fantasy.message.response;

import com.devteam.fantasy.util.PairSV;

import java.util.List;

public class BalanceResponse {
    private String title;

    private List<PairSV> pairSVList;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<PairSV> getPairSVList() {
        return pairSVList;
    }

    public void setPairSVList(List<PairSV> pairSVList) {
        this.pairSVList = pairSVList;
    }
}
