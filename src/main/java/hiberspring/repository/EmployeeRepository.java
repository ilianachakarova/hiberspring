package hiberspring.repository;

import hiberspring.domain.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee>findByFirstNameAndLastName(String firstName, String lastName);
    @Query("SELECT e FROM Employee AS e " +
            //       "INNER JOIN e.branch " +
            "WHERE e.branch.id in " +
            "(SELECT p.branch.id FROM Product AS p) " +
            "ORDER BY concat(e.firstName, ' ', e.lastName), length(e.position) DESC")
//@Query(value = "SELECT e.* " +
//        "FROM employees AS e " +
//        "INNER JOIN branches AS b " +
//        "ON e.branch_id = b.id " +
//        "INNER JOIN products AS p " +
//        "ON b.id = p.branch_id " +
//        "ORDER BY concat_ws(' ', e.first_name, e.last_name), length(e.position) DESC;", nativeQuery = true)


    List<Employee>findAllByBranchesWithAtLeastOneProduct();
}
