package io.mds.hty.taskmanager.repo;

import io.mds.hty.taskmanager.model.dao.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.lang.management.OperatingSystemMXBean;
import java.util.Optional;

@Repository
public interface CommentRepo extends JpaRepository<Comment, Long> {

    Integer deleteCommentById(Long id);
    Optional<Comment> findCommentByIdAndUserId(Long cId, Long uId);
    Integer deleteCommentByIdAndUserId(Long cId, Long uId);

}
