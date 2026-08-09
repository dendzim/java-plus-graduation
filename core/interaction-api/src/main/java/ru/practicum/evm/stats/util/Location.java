package ru.practicum.evm.stats.util;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class Location implements Serializable {
	Float lat;
	Float lon;
}
