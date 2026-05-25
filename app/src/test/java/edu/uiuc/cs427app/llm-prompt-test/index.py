import os
import traceback
import certifi
from openai import OpenAI
from dotenv import load_dotenv
from huggingface_hub import InferenceClient
load_dotenv()
model1="Qwen/Qwen2.5-Coder-32B-Instruct:fastest"
model2="Qwen/Qwen3-Coder-Next:novita"
model2r="Qwen/Qwen3-Coder-Next"

apikey = os.getenv("hf_token1")
openrouter_url= "https://openrouter.ai/api/v1"
hf_url = "https://router.huggingface.co/v1"
os.environ["SSL_CERT_FILE"] = certifi.where()
def get_completion(prompts: list[str]):
    try:
        print(f"API Key: {apikey}")
        # client = OpenAI(
        #     base_url=openrouter_url,
        #     api_key=apikey
        # )
        client = InferenceClient(
            model=model2,
            api_key=apikey
        )
        print("pass openai")
        messages = []
        for i, prompt in enumerate(prompts):
            if i ==0:
                messages.append({"role": "system", "content": prompt})
            else:
                messages.append({"role": "user", "content": prompt})
            completion = client.chat.completions.create(
                temperature=0.3,
                messages=messages
            )
            reply = completion.choices[0].message.content
            messages.append({"role": "assistant", "content": reply})
        #return messages[-1]
        return messages
    except Exception as e:
        traceback.print_exc()
        return None
def displayAllResponse(messages: list[dict]):
    for message in messages:
        print(f"{message['role']}: {message['content']}")


prompt=["""
You are an expert Android testing engineer specializing in Espresso UI tests with Mockito and Room database mocking.

I need you to generate Espresso test cases for SignIn and SignUp functionality of my Android app. The tests should follow JUnit4 format with AndroidJUnit4 runner.
## App Context:
- Main Application class: `GlobalApp extends Application`
- Database has `UserDao` with methods: `getUserByUserName(String username)`, `getUserByUserNameAndPassword(String username, String password)`, `insertUser(User user)`
- `User` entity has fields: username, password, customUi (theme: sky/beach/cyber)

## Repository Logic (AuthenRepository):
### SignUp logic:
- Checks if username exists via `userDao.getUserByUserName()`
- If NOT exists: creates new User, inserts via `userDao.insertUser()`, posts "SignUp success: User registered" to `noticeStatus`
- If exists: posts "SignUp failed: username is already registered" to `noticeStatus`

### SignIn logic:
- Checks credentials via `userDao.getUserByUserNameAndPassword()`
- If found: posts User to `noticeUser`, clears `noticeStatus` (triggers navigation to main page)
- If not found: posts "SignIn failed: Invalid username or password" to `noticeStatus`

### simplify logic of SignUp and SignIn in coding format:
public class GlobalApp extends Application {
    public User currentUser; //assigned authenticated user after SignIn success
    public AppDatabase db =  AppDatabase.getInstance(this);
}

public class AuthenRepository {
    UserDao userDao;
    ExecutorService executorService;
    public MutableLiveData<String> noticeStatus = new MutableLiveData<>("");
    public MutableLiveData<User> noticeUser = new MutableLiveData<>();
    public AuthenRepository(AppDatabase db){
        userDao = db.userDao();
        executorService = Executors.newSingleThreadExecutor();
    }
    //noticeStatus is get new value state, and notice front end in AuthenActivity
    public void SignUp(String username,String password,String  customUi){
        noticeStatus.postValue("Loading...");
        executorService.execute(()-> {
            try{
                if(userDao.getUserByUserName(username) ==null){
                    User newUser = new User(username,password,customUi);
                    userDao.insertUser(newUser);
                    noticeStatus.postValue("SignUp success: User registered");
                    return;
                }
                noticeStatus.postValue("SignUp failed: "+"username is already registered");
            }
            catch(Exception e)
                noticeStatus.postValue("SignUp failed: "+e.getMessage());
            
        });
    }
    public void SignIn(String username,String password){
        noticeUser.postValue(null);
        noticeStatus.postValue("Loading...");
        executorService.execute(()-> {
            try{
                if(userDao.getUserByUserNameAndPassword(username,password) !=null){
                    User user = userDao.getUserByUserNameAndPassword(username,password);
                    noticeUser.postValue(user); // move to main page,update currentUser in GlobalApp, no care about noticeStatus
                    noticeStatus.postValue("");
                    return;
                }
                noticeStatus.postValue("SignIn failed: "+"Invalid username or password");
            }
            catch(Exception e)
                noticeStatus.postValue("SignIn failed: "+e.getMessage());
        });
    }


## UI Components (SignUp Page - R.layout.signup):
- `R.id.inputUserName` - EditText for username (validation: must contain both letters and numbers)
- `R.id.inputPassword` - EditText for password (validation: minimum 8 characters)
- `R.id.inputCustomUi` - EditText for theme (sky/beach/cyber - don't stress test this)
- `R.id.signupButton` - Button to submit
- `R.id.signinButton` - TextView to navigate to SignIn page
- `R.id.notice_frame` - FrameLayout containing `R.id.notice_text` for status messages, 

## UI Components (SignIn Page - R.layout.signin):
- `R.id.username_input` - EditText for username
- `R.id.password_input` - EditText for password  
- `R.id.submit_button` - Button to submit
- `R.id.textRegister` - TextView to navigate to SignUp page
- `R.id.notice_frame` - FrameLayout containing `R.id.notice_text` for error messages

## Note for `R.id.notice_frame`
- R.id.notice_frame become visibility GONE if SignIn or SignUp success, so don't do check onView(withid(R.id.notice_text)). do userDao() check is already sufficient   


## Application Flow:
1. First launch, view layout default is SignIn page. Can navigate to SignUp page by R.id.textRegister
2. On SignUp success, `AuthenActivity` navigates to SignIn page automatically
3. On SignIn success (noticeUser observed), app navigates to main page (not part of these tests)

## GlobalApp and Database:
- Use `GlobalApp` instance to access database in tests, e.g. `((GlobalApp) getApplicationContext()).db.userDao()`


## Required Test Cases:
### SignUp Tests (focus on `showSignup()` page):
1. **TEST_SIGNUP_WRONG_FORMAT** - Username and password fails validation (no letter or no number)
2. **TEST_SIGNUP_SUCCESS** - Valid username (letters+numbers) and valid password (8+ chars)
3. **TEST_SIGNUP_USERNAME_ALREADY_EXISTS**  - follow same format as TEST_SIGNUP_SUCCESS, but second registration use same username(username must contain both word and number), but different password. later do assertion with userDao()

### SignIn Tests (focus on `showSignin()` page):
1. **TEST_SIGNIN_WRONG_CREDENTIALS** - Invalid username/password combination
2. **TEST_SIGNIN_SUCCESS** - Valid credentials should trigger `noticeUser` update
3. **TEST_SIGNIN_EMPTY_FIELDS** - Empty username or password fields
4. **TEST_SIGN_NAVIGATION** - Click `R.id.textRegister` navigates to SignUp page, then click `R.id.signinButton` navigates back to SignIn page

## Can use following Structure Requirements: (can be combine them in test cases, not necessary to have all of them in each test case)
- Use `@RunWith(AndroidJUnit4.class)` and `@Before` setup
- Use `GlobalApp` instance for database access
- Use `onView(withId(R.id.xxx))` for UI interactions
- Use `perform(typeText()), closeSoftKeyboard(), click()`
- Use `Thread.sleep(500)` after submit/click to wait for database operation (since it's async)
- Use `assertNotNull()`, `assertNull()` for database assertions
- Use `onView().check(matches(isDisplayed()))` for UI feedback assertions

## Generate the complete test class and method for all required test cases.

Format the response as a single Java code block with the complete test class.
""",
""" mostly correct, write back test cases, but modified some following sections:
- put Thread.sleep as separate function to make it more clear
- In TEST_SIGNUP_WRONG_FORMAT(), don't use onView(withId(R.id.notice_text)) ui check. Can check still exist of R.id.signinButton or getUserByUserName(String username) is sufficent
- In TEST_SIGNIN_WRONG_CREDENTIALS(),although there is mismatch between user in database and ui signup.Still make (username input contain both letter and number)(password input at least 8 characters).
- In TEST_SIGNIN_EMPTY_FIELDS() simplied it to be only check case of empty password. No need check empty for both fields.
- In TEST_SIGN_NAVIGATION(), only one time from (SignIn to Signup page, then back to SignIn page).
"""]

result = get_completion(prompt)
#print(result['content'] if result else "No response from model")
print(displayAllResponse(result) if result else "No response from model")
