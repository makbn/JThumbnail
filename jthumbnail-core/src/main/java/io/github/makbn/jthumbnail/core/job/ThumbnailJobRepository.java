package io.github.makbn.jthumbnail.core.job;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThumbnailJobRepository extends JpaRepository<ThumbnailJob, String> {

    List<ThumbnailJob> findByStatusOrderByCreatedAtDesc(ThumbnailJob.JobStatus status);

    List<ThumbnailJob> findAllByOrderByCreatedAtDesc();
}
