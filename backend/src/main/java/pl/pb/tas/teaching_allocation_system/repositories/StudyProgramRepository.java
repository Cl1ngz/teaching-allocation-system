package pl.pb.tas.teaching_allocation_system.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.pb.tas.teaching_allocation_system.models.entities.StudyProgram;

public interface StudyProgramRepository extends JpaRepository<StudyProgram, Long> {

	Optional<StudyProgram> findByCode(String code);
}
