package com.etdp.etdp.data;

import com.google.gson.Gson;

public class JsonConverter {

	public String toJson() {
		return new Gson().toJson(this);
	}

	public String toString() {
		return toJson();
	}

}
