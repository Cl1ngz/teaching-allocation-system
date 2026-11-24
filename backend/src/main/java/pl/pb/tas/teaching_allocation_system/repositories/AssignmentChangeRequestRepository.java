package pl.pb.tas.teaching_allocation_system.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.pb.tas.teaching_allocation_system.models.entities.AssignmentChangeRequest;
import pl.pb.tas.teaching_allocation_system.models.enums.ApprovalStatus;

public interface AssignmentChangeRequestRepository extends JpaRepository<AssignmentChangeRequest, Long> {

	List<AssignmentChangeRequest> findByStatusOrderByRequestedAtAsc(ApprovalStatus status);
}
