package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {
  
  def index = Action {
        // List<User> users = User.findAll();
        // List<Wishlist> wishlists= Wishlist.findAll();
    Ok(views.html.index("Your new application is ready."))
  }

    
   def about = TODO
   
   def contact = TODO
  
}