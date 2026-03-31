package com.example.demochatbox.repository;

import com.example.demochatbox.model.ViewHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Long> {

    @Query("""
        select vh from ViewHistory vh
        where vh.user.id = :userId
        order by vh.viewedAt desc
        """)
    List<ViewHistory> findRecentByUserId(@Param("userId") Long userId);
}
