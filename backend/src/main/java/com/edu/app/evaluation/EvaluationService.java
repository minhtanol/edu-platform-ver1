package com.edu.app.evaluation;

import com.edu.app.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class EvaluationService {
  private final EvaluationRepository evaluations; private final EvaluationHistoryRepository histories; private final UserRepository users;
  @Transactional public EvaluationDtos.EvaluationResponse create(String teacherEmail, EvaluationDtos.EvaluationRequest req) {
    var e = new Evaluation(); e.setTeacher(users.findByEmailAndDeletedAtIsNull(teacherEmail).orElseThrow()); e.setStudent(users.findById(req.studentId()).orElseThrow()); e.setSubject(req.subject()); e.setContent(req.content()); e.setScore(req.score()); return toDto(evaluations.save(e));
  }
  public Page<EvaluationDtos.EvaluationResponse> byStudent(UUID id, Pageable p) { return evaluations.findByStudentIdAndDeletedAtIsNull(id, p).map(this::toDto); }
  @Transactional public EvaluationDtos.EvaluationResponse update(UUID id, EvaluationDtos.EvaluationRequest req) {
    var e = evaluations.findById(id).orElseThrow(); var h = new EvaluationHistory(); h.setEvaluation(e); h.setPreviousContent(e.getContent()); h.setPreviousScore(e.getScore()); histories.save(h);
    e.setSubject(req.subject()); e.setContent(req.content()); e.setScore(req.score()); return toDto(e);
  }
  @Transactional public void delete(UUID id) { var e = evaluations.findById(id).orElseThrow(); e.setDeletedAt(Instant.now()); }
  private EvaluationDtos.EvaluationResponse toDto(Evaluation e) { return new EvaluationDtos.EvaluationResponse(e.getId(), e.getStudent().getId(), e.getTeacher().getId(), e.getSubject(), e.getContent(), e.getScore()); }
}
