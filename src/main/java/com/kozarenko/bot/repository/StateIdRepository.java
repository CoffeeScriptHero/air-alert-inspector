package com.kozarenko.bot.repository;

import com.kozarenko.bot.model.StateId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StateIdRepository extends JpaRepository<StateId, Long> {
}
