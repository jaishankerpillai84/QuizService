package com.jaypillai.quizapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jaypillai.pojo.LoginData;
import com.jaypillai.pojo.QuizData;

@Path("/quizapp/*")
public class Services {
	private SessionFactory sessionFactory;
	public Services() {
		sessionFactory = new Configuration().configure().buildSessionFactory();
	}
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String DefaultMessage() {
		return "Welcome to quiz app.";
	}
	@Path("authenticate_user/")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String AuthenticateUser(@QueryParam("username") String username,
			@QueryParam("password") String password) {
		System.out.println("Inside AuthenticateUser with username:" + username + " and password:" + password);
		Session session = sessionFactory.openSession();
		Query query = session.createQuery("FROM LoginData where Username=:q");
		query.setParameter("q", username);
		List<Object> results = query.list();
		System.out.println("Number of results obtained is " + results.size());
		boolean authenticate = false;
		for(Object result : results) {
			LoginData data = (LoginData)result;
			System.out.println("Result is username:" + data.getUsername() + " password:" + data.getPassword());
			if(data.getPassword().equals(password)) {
				authenticate = true;
			}
		}
		System.out.println("Authentication is " + authenticate);
		return authenticate ? "true" : "false";
	}
	@Path("/get_quiz_list")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String  GetQuizList() {
		System.out.println("Inside GetQuizList");
		Session session = sessionFactory.openSession();
		Query query = session.createQuery("SELECT distinct quizName FROM QuizData");
		List<String> list = query.list();
		for(String q : list) {
			System.out.println(q);
		}
		return new Gson().toJson(list);
	}
	@Path("add_question")
	@GET
	@Produces("application/json")
	public String AddQuestions(@QueryParam("quizname") String quizName) {
		System.out.println("Adding question to " + quizName);
		// Generate question.
		Random rand = new Random();
		String question = "Question No " + Integer.toString(rand.nextInt());
		ArrayList<String> choices = new ArrayList<String>();
		choices.add(Integer.toString(rand.nextInt()));
		choices.add(Integer.toString(rand.nextInt()));
		choices.add(Integer.toString(rand.nextInt()));
		choices.add(Integer.toString(rand.nextInt()));
		String answer = choices.get(rand.nextInt(4));
		QuizData newData = new QuizData();
		newData.setQuestion(question);
		newData.setAnswer(answer);
		newData.setChoices(choices);
		newData.setQuizName(quizName);
		
		// Save to database.
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		session.save(newData);
		transaction.commit();
		session.close();
		
		return new Gson().toJson(newData);
	}
	@Path("get_questions/")
	@GET
	@Produces("application/json")
	public String GetQuestions(@QueryParam("quizname") String quizName) {
		System.out.println("Getting questions of "+ quizName);
		Session session = sessionFactory.openSession();
		Query query = session.createQuery("FROM QuizData WHERE quizName=:q");
		query.setParameter("q", quizName);
		List<QuizData> list = query.list();
		return new Gson().toJson(list);
	}
}
