package com.devteam.fantasy.service;

import java.util.List;

import com.devteam.fantasy.message.response.ApuestasActivasResponse;

public interface AdminService {

	List<ApuestasActivasResponse> getSorteosListWithMoneda(String currency);
}
