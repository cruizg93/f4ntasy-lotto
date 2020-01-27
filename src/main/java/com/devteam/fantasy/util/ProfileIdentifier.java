package com.devteam.fantasy.util;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ProfileIdentifier {
	
	@Autowired
    Environment env;
	
	public boolean isProfileProdActive() {
		
		String[] activeProfiles = env.getActiveProfiles();
		if ( activeProfiles != null && activeProfiles.length>0) {
			if( !Arrays.asList(activeProfiles).contains("prod")) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isProfileUATActive() {
		
		String[] activeProfiles = env.getActiveProfiles();
		if ( activeProfiles != null && activeProfiles.length>0) {
			if( !Arrays.asList(activeProfiles).contains("uat")) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isProfileDevActive() {
		
		String[] activeProfiles = env.getActiveProfiles();
		if ( activeProfiles != null && activeProfiles.length>0) {
			if( !Arrays.asList(activeProfiles).contains("dev")) {
				return true;
			}
		}
		return false;
	}
	
}
