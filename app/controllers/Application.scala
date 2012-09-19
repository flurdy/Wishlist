package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._

object Application extends Controller with Secured{


	val ValidEmailAddress = """^[0-9a-zA-Z]([+-_\.\w]*[0-9a-zA-Z])*@([0-9a-zA-Z][-\w]*[0-9a-zA-Z]\.)+[a-zA-Z]{2,9}$""".r

	val simpleRegisterForm = Form {
		"email" -> optional(text(maxLength = 99))
 	}	

 	val registerForm = Form(
    tuple(
      "username" -> nonEmptyText(maxLength = 99),
      "fullname" -> optional(text(maxLength = 99)),
      "email" -> nonEmptyText(maxLength = 99),
      "password" -> nonEmptyText(minLength = 4, maxLength = 99),
      "confirm" -> nonEmptyText(minLength = 4, maxLength = 99)
    ) verifying("Passwords do not match", fields => fields match {
      case (username, fullname, email, password, confirmPassword) => {
        password.trim == confirmPassword.trim
     }
    }) verifying("Username is already taken", fields => fields match {
     case (username, fullname, email, password, confirmPassword) => {
        !Recipient.findByUsername(username.trim).isDefined
     }
    }) verifying("Email address is not valid", fields => fields match {
      case (username, fullname, email, password, confirmPassword) => {
      	ValidEmailAddress.findFirstIn(email.trim).isDefined
      }
    })
  )

	val loginForm = Form(
	   tuple(
	      "username" -> nonEmptyText(maxLength = 99),
	      "password" -> nonEmptyText(maxLength = 99),
      	"source" -> optional(text)
	    ) verifying("Log in failed. Username does not exist or password is invalid", fields => fields match {
	      case (username, password, source) => Recipient.authenticate(username, password).isDefined
    	}) 
	)	

	def index = Action { implicit request =>
    currentParticipant match {
      case Some(recipient) => Ok(views.html.indexrecipient(WishController.simpleCreateWishlistForm))
      case None => Ok(views.html.indexanon())
    }
	}

  	def register = Action { implicit request =>
  		registerForm.bindFromRequest.fold(
        errors => {
          Logger.warn("Registration failed: " + errors)
          BadRequest(views.html.register(errors))
        },
   	   registeredForm => {
	      	Logger.info("New registration: " + registeredForm._1)

	      	val recipient = Recipient(None,registeredForm._1,registeredForm._2,registeredForm._3,Some(registeredForm._4))
	      		
	      	recipient.save	
	      	
	      	// TODO: Send email confirmation

         	Redirect(routes.Application.index()).withSession(
          "username" -> registeredForm._1).flashing("message"-> "Welcome, you have successfully registered")
      	}
      )
  }

  def redirectToRegisterForm = Action { implicit request =>
  		simpleRegisterForm.bindFromRequest.fold(
        errors => {
          BadRequest(views.html.register(registerForm))
        },
   	   emailInForm => {
   	   	emailInForm match {
   	   		case None => Ok(views.html.register(registerForm))
   	   		case Some(email) => {   	   	
		         	Ok(views.html.register(	
		         			registerForm.fill( email, None, email, "", "") ) )		
   	   		}
   	   	}
      	}
      )
  }

	def showRegisterForm = Action { implicit request =>
		Ok(views.html.register(registerForm))
	}

	def showLoginForm = Action { implicit request =>
		Ok(views.html.login(loginForm))
	}

	def login = Action { implicit request =>
		loginForm.bindFromRequest.fold(
			errors => {
				Logger.info("Log in failed:"+ errors)
				BadRequest(views.html.login(errors))
			},
			loggedInForm => {
				Logger.debug("Logging in: " + loggedInForm._1)
				Redirect(routes.Application.index()).withSession(
					"username" -> loggedInForm._1).flashing("message"->"You have logged in")
			}
		)		
	}
    
   def showResetPassword = TODO 

   def about = Action { implicit request =>
    Ok(views.html.about())
  }
   
   def contact = Action { implicit request =>
    Ok(views.html.contact())
  }

   def logout = Action {
      Redirect(routes.Application.index).withNewSession.flashing("message"->"You have been logged out")
   }
  
}



trait Secured {

  	def username(request: RequestHeader) = request.session.get(Security.username)

  	def isAuthenticated(f: => String => Request[AnyContent] => Result) = {
    	Security.Authenticated(username, onUnauthenticated) { username =>
      	  Action(request => f(username)(request))
   	}
  	}
	
	private def onUnauthenticated(request: RequestHeader) = {
		Results.Redirect(routes.Application.showLoginForm)      
  	}

	implicit def currentParticipant(implicit session: Session): Option[Recipient] = {
		session.get(Security.username) match { 
			case None => None
			case Some(sessionUsername) => Recipient.findByUsername( sessionUsername )
		}
	}

  def withCurrentRecipient(f: Recipient => Request[AnyContent] => Result) = isAuthenticated {
    username => implicit request =>
      Recipient.findByUsername(username).map {
        recipient => f(recipient)(request)
      }.getOrElse(onUnauthenticated(request))
  }


}











