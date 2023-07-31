package com;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MyUserRepository extends JpaRepository<MyUser, String> {

    MyUser findByUsername(String username);

    MyUser findByIsHost(boolean isHost);

    List<MyUser> findAllByOrderBySeatOrder();

    MyUser findBySeatOrder(int seatOrder);

    List<MyUser> findAllByUsernameIn(Collection<String> usernames);

    @Query("select u from MyUser u where u <> :user")
    List<MyUser> findAllByNot(@Param("user") MyUser user);


    @Query("select u from MyUser u where u not in (:users)")
    List<MyUser> findByUserNotIn(@Param("users") Collection<MyUser> users);


}


