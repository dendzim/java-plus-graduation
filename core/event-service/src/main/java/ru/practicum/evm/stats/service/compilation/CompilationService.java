package ru.practicum.evm.stats.service.compilation;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.evm.stats.dto.compilation.CompilationDto;
import ru.practicum.evm.stats.dto.compilation.CompilationSearchFilter;
import ru.practicum.evm.stats.dto.compilation.CompilationUpdateDto;
import ru.practicum.evm.stats.dto.compilation.NewCompilationDto;

import java.util.List;

public interface CompilationService {

	CompilationDto getById(Long compilationId, HttpServletRequest request);

	void delById(Long compilationId);

	CompilationDto addCompilation(NewCompilationDto compilation);

	CompilationDto updateCompilation(Long compilationId, CompilationUpdateDto compilation);

	List<CompilationDto> getByFilter(CompilationSearchFilter filter, HttpServletRequest request);
}
