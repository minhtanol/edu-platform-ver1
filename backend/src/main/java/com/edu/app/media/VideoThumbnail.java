package com.edu.app.media;

import com.edu.app.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name="video_thumbnails")
@Getter @Setter
public class VideoThumbnail extends BaseEntity {
  @ManyToOne(optional=false) private Media media;
  @Column(nullable=false, length = 1000) private String path;
}
