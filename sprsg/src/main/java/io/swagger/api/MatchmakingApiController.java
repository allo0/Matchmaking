package io.swagger.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import io.swagger.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-01-10T13:05:51.960Z[GMT]")
@Controller
public class MatchmakingApiController implements MatchmakingApi {

	private static final Logger log = LoggerFactory.getLogger(MatchmakingApiController.class);

	private final ObjectMapper objectMapper;

	private final HttpServletRequest request;

	@org.springframework.beans.factory.annotation.Autowired
	public MatchmakingApiController(ObjectMapper objectMapper, HttpServletRequest request) {
		this.objectMapper = objectMapper;
		this.request = request;
	}

	public ResponseEntity<List<UserPairAssignment>> matchmakingPost(
			@ApiParam(value = "The body is a JSON structure having the following parts {a} global user score {b} pairwise user scores and {c} user-to-user collaboration intentions. The output of the computation is a user pair assignment matrix.", required = true) @Valid @RequestBody Body body) {
		String accept = request.getHeader("Accept");
		if (accept != null && accept.contains("application/json")) {
			ArrayList<UserPairAssignment> result = new ArrayList<UserPairAssignment>();



			MatchmakingAlgorithmImplementation ma = new MatchmakingAlgorithmImplementation();

			try {
				result = ma.final_pair(body.getUserGlobalScores(), body.getUserPairwiseScore(),
						body.getUserCollaborationIntentions());

				String user1;
				String user2;
				System.out.println("The final pairs: ");
				for (int i = 0; i < result.size(); i++) {
					user1 = result.get(i).getUser1();
					user2 = result.get(i).getUser2();
					System.out.println(user1 + " " + user2);
				}
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return new ResponseEntity<List<UserPairAssignment>>(result, HttpStatus.OK);
		}

		return new ResponseEntity<List<UserPairAssignment>>(HttpStatus.NOT_IMPLEMENTED);
	}
}
