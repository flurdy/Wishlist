package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._



object WishController extends Controller with Secured {


    val simpleCreateWishlistForm = Form {
        "title" -> text(maxLength = 180,minLength = 2 )
    }   


    val editWishlistForm = Form(
        tuple(
          "title" -> nonEmptyText(maxLength = 180),
          "description" -> optional(text(maxLength = 2000))
        )
    )

    val searchForm = Form {
        "term" -> optional(text(maxLength = 99))
    }   

    val simpleAddWishForm = Form {
        "title" -> text(maxLength = 180,minLength = 2 )
    }   


    def create(username:String) = withCurrentDreamer { currentDreamer => implicit request =>
        simpleCreateWishlistForm.bindFromRequest.fold(
            errors => {
              Logger.warn("Create failed: " + errors)
              BadRequest(views.html.application.indexdreamer(errors))
            },
           titleForm => {
                Logger.info("New wishlist: " + titleForm)

                val wishlist = new Wishlist(None, titleForm.trim, None, currentDreamer, currentDreamer).save

                Redirect(routes.WishController.showEditWishlist(username,wishlist.wishlistId.get)).flashing("message" -> "Wishlist created")
            }
        )
    }

    def showEditWishlist(username:String,wishlistId:Long) =  withCurrentDreamer { currentDreamer => implicit request =>
        val wishlist = Wishlist.findById(wishlistId).get
        val editForm = editWishlistForm.fill((wishlist.title,wishlist.description))
        val wishes = Wishlist.findWishesForWishlist(wishlist)
        Ok(views.html.wishlist.editwishlist(wishlist, wishes, editForm, simpleAddWishForm))
    }

    def updateWishlist(username:String,wishlistId:Long) = withCurrentDreamer { currentDreamer => implicit request =>
        val wishlist = Wishlist.findById(wishlistId).get
        val wishes = Wishlist.findWishesForWishlist(wishlist)
        editWishlistForm.bindFromRequest.fold(
            errors => {
              Logger.warn("Update failed: " + errors)
              BadRequest(views.html.wishlist.editwishlist(wishlist,wishes,errors,simpleAddWishForm))
            }, 
            editForm => {
                Logger.info("Updating wishlist: " + editForm)

                val updatedWishlist = wishlist.copy(title=editForm._1,description=editForm._2)

                updatedWishlist.update

                Redirect(routes.WishController.showEditWishlist(username,wishlist.wishlistId.get)).flashing("message" -> "Wishlist updated")
            }
        )
    }

    def deleteWishlist(username:String,wishlistId:Long) = withCurrentDreamer { currentDreamer => implicit request =>
        Logger.info("Deleting wishlist: " + wishlistId)

        Wishlist.findById(wishlistId).get.delete

        Redirect(routes.Application.index()).flashing("messageWarning" -> "Wishlist deleted")
    }


    def showWishlist(username:String,wishlistId:Long) =  withCurrentDreamer { currentDreamer => implicit request =>
        val wishlist = Wishlist.findById(wishlistId).get
        Ok(views.html.wishlist.showwishlist(wishlist))
    }

    def showConfirmDeleteWishlist(username:String,wishlistId:Long) = withCurrentDreamer { currentDreamer => implicit request =>
        val wishlist = Wishlist.findById(wishlistId).get
        Ok(views.html.wishlist.deletewishlist(wishlist))
    }

    def listWishlists(username:String) = TODO

    def search = Action { implicit request =>
        searchForm.bindFromRequest.fold(
            errors => {
              Logger.warn("Update failed: " + errors)
              BadRequest
            }, 
            term => {
                val wishlists = term match {
                    case None => Wishlist.findAll
                    case Some(searchTerm) => Wishlist.searchForWishlistsContaining(searchTerm)
                }
                Ok(views.html.wishlist.listwishlists(wishlists,searchForm.fill(term)))
            }
        )   
   }

    def addWishToWishlist(username:String,wishlistId:Long) = withCurrentDreamer { currentDreamer => implicit request =>
        simpleAddWishForm.bindFromRequest.fold(
            errors => {
                Logger.warn("Add failed: " + errors)
                BadRequest
            }, 
            title => {
                val wishlist = Wishlist.findById(wishlistId).get

                val wish = Wish(None,title,None,Some(wishlist))

                wish.save

                Redirect(routes.WishController.showEditWishlist(username,wishlist.wishlistId.get)).flashing("message" -> "Wish added")
            }
        )   
   }

   def deleteWishFromWishlist(username:String,wishlistId:Long,wishId:Long) = withCurrentDreamer { currentDreamer => implicit request =>

        val wishlist = Wishlist.findById(wishlistId).get

        val wish = Wish.findById(wishId).get

        wish.delete       

        Redirect(routes.WishController.showEditWishlist(username,wishlist.wishlistId.get)).flashing("message" -> "Wish deleted")
   }



}


/*

import models.User;
import models.Wish;
import models.Wishlist;
import play.mvc.Controller;

import java.util.List;

public class WishController extends Controller {


    public static void listWishlists(String username){
        final User recipient = User.find("byUsername",username).first();
        final List<Wishlist> wishlists= Wishlist.find("byRecipient",recipient).fetch();
        render(recipient,wishlists);
    }


    public static void showWishlist(String username, Long listId){
        final User recipient = User.find("byUsername",username).first();
        final Wishlist wishlist= Wishlist.findById(listId);
        render(recipient,wishlist);
    }



    public static void showWish(String username, Long listId, Long wishId){
        final User recipient = User.find("byUsername",username).first();
        final Wishlist wishlist= Wishlist.findById(listId);
        final Wish wish= Wish.findById(wishId);
        render(recipient,wishlist,wish);
    }


}
*/