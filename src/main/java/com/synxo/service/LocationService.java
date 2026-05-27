package com.synxo.service;

import com.synxo.service.model.Coordinates;

public interface LocationService {

	Coordinates resolveCity(String city);

	Coordinates preciseCoordinates(Double latitude, Double longitude);
}
