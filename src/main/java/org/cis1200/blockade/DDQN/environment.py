from enum import Enum
import torch


class Environment:

    def __init__(self, game_board_length, char1, char2):
        self.game_board_length = game_board_length
        self.game_board = torch.zeros((game_board_length, game_board_length), dtype=torch.uint8)
        self.char1 = char1
        self.char2 = char2
        self.prev_state = None
        self.prev_action = None

    def get_state(self):
        c1b = self.__xy2bin(self.char1)
        c2b = self.__xy2bin(self.char2)
        state = torch.cat((self.game_board.flatten(), c1b, c2b))
        return state.clone()

    def __xy2bin(self, xy):
        bit_length = (self.game_board_length ** 2 - 1).bit_length()
        x, y = xy
        c = x * self.game_board_length + y
        cl = [int(x) for x in bin(c)[2:]]
        cl_t = torch.tensor(cl, dtype=torch.uint8)
        z = torch.zeros(bit_length - cl_t.size(0), dtype=torch.uint8)
        return torch.cat((z, cl_t))

    def __bin2xy(self, pos):
        binary_string = ''.join(map(str, pos.tolist()))
        i = int(binary_string, 2)
        y = i % self.game_board_length
        x = i // self.game_board_length
        return x, y

    # returns list of transition:
    # initial state, action, opponent action (None if the game is over by then), reward, next state (None if game ended)
    def step(self, qmodel):
        s_o = self.get_state()
        o = qmodel.predict(s_o)
        char1_status = self.__step_helper(o)
        if self.prev_state is None:
            self.prev_state = s_o
            self.prev_action = o
            return self.step(qmodel)
        transitions = []
        if char1_status == Status.DEAD:
            t1 = self.prev_state, self.prev_action, o, 1, None
            t2 = s_o, o, None, -1, None
            transitions.append(t1)
            transitions.append(t2)
        elif char1_status == Status.TIED:
            t1 = self.prev_state, self.prev_action, o, 0, None
            t2 = s_o, o, None, 0, None
            transitions.append(t1)
            transitions.append(t2)
        else:
            t = self.prev_state, self.prev_action, o, 0, self.get_state()
            transitions.append(t)
        game_alive = char1_status == Status.ALIVE
        self.prev_state = s_o
        self.prev_action = o
        return game_alive, transitions

    def __step_helper(self, action):
        x, y = self.char1
        self.game_board[x, y] = 1
        i = torch.argmax(action)
        match i:
            case 0:
                x = x - 1
            case 1:
                x = x + 1
            case 3:
                y = y - 1
            case 4:
                y = y + 1
        self.char1 = x, y
        b = self.__check_alive()
        c = self.char1
        self.char1 = self.char2
        self.char2 = c
        return b

    def __check_alive(self):
        x, y = self.char1
        if x < 0 or x >= self.game_board_length or y < 0 or y >= self.game_board_length:
            return Status.DEAD
        if self.game_board[x, y] == 1:
            return Status.DEAD
        x2, y2 = self.char2
        if x == x2 and y == y2:
            return Status.TIED
        return Status.ALIVE


class Status(Enum):
    ALIVE = 1
    DEAD = 2
    TIED = 3



