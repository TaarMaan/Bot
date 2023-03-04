package com.example.bot.reposytory;

import com.example.bot.model.Diary;
import org.springframework.data.repository.CrudRepository;

public interface DiaryRepository extends CrudRepository<Diary, Long> {
}
