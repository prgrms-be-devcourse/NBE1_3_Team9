package com.grepp.nbe1_3_team9.domain.repository.finance

import com.grepp.nbe1_3_team9.domain.entity.finance.FinancialPlan
import com.grepp.nbe1_3_team9.domain.entity.group.Group
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface FinancialPlanRepository :JpaRepository<FinancialPlan, Long> {

    fun findAllByGroup_GroupId(groupId: Long): List<FinancialPlan>
    @Modifying
    @Query("SELECT itemName from FinancialPlan where group =:group")
    fun findFinancialPlanItems(group: Group): List<String>
}