package com.markokosic.minicrm.modules.user;

import com.markokosic.minicrm.modules.user.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT * FROM users WHERE email = :email AND status = 'ACTIVE'", nativeQuery = true)
    Optional<User> findByEmail(@Param("email") String email);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM users WHERE email = :email AND status = 'ACTIVE'", nativeQuery = true)
    boolean existsByEmail(@Param("email") String email);

    List<User> findAllByStatus(UserStatus status);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO users (email, first_name, last_name, password, tenant_id, roles, must_change_password, status) VALUES (:email, :firstName, :lastName, :password, :tenantId, :roles, :mustChangePassword, 'ACTIVE')", nativeQuery = true)
    void insertUser(
        @Param("email") String email,
        @Param("firstName") String firstName,
        @Param("lastName") String lastName,
        @Param("password") String password,
        @Param("tenantId") Long tenantId,
        @Param("roles") String roles,
        @Param("mustChangePassword") boolean mustChangePassword
    );
}
