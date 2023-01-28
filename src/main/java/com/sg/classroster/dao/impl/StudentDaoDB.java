package com.sg.classroster.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sg.classroster.dao.StudentDao;
import com.sg.classroster.entity.Student;

@Repository
public class StudentDaoDB implements StudentDao {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public Student getStudentById(int id) {
		try {
			final String SELECT_STUDENT_BY_ID = "SELECT * FROM student WHERE id = ?";
			return jdbcTemplate.queryForObject(SELECT_STUDENT_BY_ID, new StudentMapper(), id);
		} catch (DataAccessException ex) {
			return null;
		}
	}

	@Override
	public List<Student> getAllStudents() {
		final String SELECT_ALL_STUDENTS = "SELECT * FROM student";
		return jdbcTemplate.query(SELECT_ALL_STUDENTS, new StudentMapper());
	}

	@Override
	@Transactional
	public Student addStudent(Student student) {
		final String INSERT_STUDENT = "INSERT INTO student(firstName, lastName) VALUES(?, ?)";
		jdbcTemplate.update(INSERT_STUDENT, student.getFirstName(), student.getLastName());

		int newId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
		student.setId(newId);
		return student;
	}

	@Override
	public void updateStudent(Student student) {
		final String UPDATE_STUDENT = "UPDATE student SET firstName = ?, lastName = ? WHERE id = ?";
		jdbcTemplate.update(UPDATE_STUDENT, student.getFirstName(), student.getLastName(), student.getId());		
	}

	@Override
	@Transactional
	public void deleteStudentById(int id) {
		final String DELETE_COURSE_STUDENT = "DELETE FROM course_student WHERE studentId = ?";
		jdbcTemplate.update(DELETE_COURSE_STUDENT, id);

		final String DELETE_STUDENT = "DELETE FROM student WHERE id = ?";
		jdbcTemplate.update(DELETE_STUDENT, id);
	}
	
	public static final class StudentMapper implements RowMapper<Student> {

		@Override
		public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
			Student student = new Student();
			student.setId(rs.getInt("id"));
			student.setFirstName(rs.getString("firstName"));
			student.setLastName(rs.getString("lastName"));
			
			return student;
		}
		
	}

}
