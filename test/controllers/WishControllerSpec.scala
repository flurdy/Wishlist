package controllers

import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.{any, anyString, eq => eqTo}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._
import scala.concurrent.Future
import com.flurdy.wishlist.ScalaSoup
import models._
import repositories._


class WishControllerSpec extends BaseUnitSpec with Results with GuiceOneAppPerSuite {

   trait Setup {
      val configurationMock = mock[Configuration]
      val recipientRepositoryMock = mock[RecipientRepository]
      val recipientLookupMock = mock[RecipientLookup]
      val wishlistRepositoryMock = mock[WishlistRepository]
      val wishlistLookupMock = mock[WishlistLookup]
      val wishLookupMock = mock[WishLookup]
      val wishRepositoryMock = mock[WishRepository]
      val wishEntryRepositoryMock = mock[WishEntryRepository]
      val reservationRepositoryMock = mock[ReservationRepository]
      val controller = new WishController(configurationMock, recipientLookupMock)(
         wishlistRepositoryMock, wishRepositoryMock, wishEntryRepositoryMock, wishlistLookupMock, wishLookupMock, reservationRepositoryMock, recipientRepositoryMock)
   }

   trait WishlistSetup extends Setup {

      val recipient = new Recipient(222).copy(username = "somerecipient")
      val wishlist  = new Wishlist(123, recipient).copy( title = "Some wishlist",
                                                         description = Some("Blah blah") )
      val wish      = new Wish(444, recipient)
      val wishes    = List(wish)

      def showAnonymousWishlist() = {

         when( wishlistLookupMock.findWishlist(122) )
               .thenReturn(Future.successful(Some(wishlist)))
//         when( wishlistRepositoryMock.inflateWishlistRecipient(wishlist)(recipientRepositoryMock))
//               .thenReturn(Future.successful(wishlist))
         when( recipientRepositoryMock.findRecipientById(222) )
               .thenReturn( Future.successful( Some( recipient ) ) )
         when( wishLookupMock.findWishes(wishlist) )
               .thenReturn( Future.successful(wishes) )

         val result = controller.showWishlist("someuser", 122).apply(FakeRequest())

         status(result) mustBe 200
         ScalaSoup.parse(contentAsString(result))

      }

//      def showRecipientWishlist() = {
//
//         val result = controller.showWishlist("someuser", 122).apply(
//            FakeRequest().withSession("username"  -> "someuser"))
//
//         status(result) mustBe 200
//         ScalaSoup.parse(contentAsString(result))
//
//      }

   }

   "Wish controller" must given {
      "not logged in" when requesting {
         "[GET] /recipient/someuser/wishlist/122/" should show {
            "show wishlist page" in new WishlistSetup {
               showAnonymousWishlist()
                     .select("#view-wishlist-page").headOption mustBe defined
            }
            "title" in new WishlistSetup {
               showAnonymousWishlist()
                     .select("#view-wishlist-page h3").headOption.value.text mustBe "Some wishlist"
            }
            "recipient" in new WishlistSetup {
               showAnonymousWishlist()
                     .select("#view-wishlist-page p a").headOption.value.text mustBe "somerecipient"
            }
            "description" in new WishlistSetup {
               showAnonymousWishlist()
                     .select("#view-wishlist-page p:eq(2)")
                     .headOption.value.text mustBe "Blah blah"
            }
            "have wishes" in new WishlistSetup {
               showAnonymousWishlist()
                     .select("#view-wishlist-page #wishlist-wishes").headOption mustBe defined
            }
            "wish" in new WishlistSetup {
               showAnonymousWishlist()
                     .select("#view-wishlist-page #wishlist-wishes ul #wish-row-444").headOption mustBe defined
            }
         }
      }
   }
}
