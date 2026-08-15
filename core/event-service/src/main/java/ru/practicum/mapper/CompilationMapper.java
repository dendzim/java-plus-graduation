package ru.practicum.mapper;

import org.mapstruct.*;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.CompilationUpdateDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.model.Compilation;

@Mapper(componentModel = "spring", uses = {EventMapper.class})
public interface CompilationMapper {

	CompilationDto toCompilationDto(Compilation compilation);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "events", ignore = true)
	Compilation toEntity(NewCompilationDto dto);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "events", ignore = true)
	void merge(@MappingTarget Compilation compilation, CompilationUpdateDto dto);
}
