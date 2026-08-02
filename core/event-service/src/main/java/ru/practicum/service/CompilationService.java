package ru.practicum.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.CompilationSearchFilter;
import ru.practicum.dto.CompilationUpdateDto;
import ru.practicum.dto.NewCompilationDto;

import java.util.List;

public interface CompilationService {

	CompilationDto getById(Long compilationId, HttpServletRequest request);

	void delById(Long compilationId);

	CompilationDto addCompilation(NewCompilationDto compilation);

	CompilationDto updateCompilation(Long compilationId, CompilationUpdateDto compilation);

	List<CompilationDto> getByFilter(CompilationSearchFilter filter, HttpServletRequest request);
}
