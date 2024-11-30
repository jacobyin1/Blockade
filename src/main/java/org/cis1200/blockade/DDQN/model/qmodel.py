import torch
import random
from torch import nn
import numpy as np


class QModel:
    def __init__(self, epsilon_function, nn_params, device='cuda'):
        self.n_train = 0
        self.e_func = epsilon_function
        i, h1, h2 = nn_params
        self.target = Net(i, h1, h2, 16).to(device)
        self.net = Net(i, h1, h2, 16).to(device)
        self.target.eval()
        self.net.train()
        self.losses = []
        self.device = device

    def predict(self, state):
        e = self.e_func(self.n_train)
        n = random.random()
        if n < e:
            b_i = random.randint(0, 3)
            b = torch.zeros(4)
            b[b_i] = 1
            return b
        state1 = state.unsqueeze(0).to(self.device)
        q_sa = self.target(state1).reshape((4, 4))
        v, i = torch.min(q_sa, dim=1)
        a_i = v.argmax()
        a = torch.zeros(4)
        a[a_i] = 1
        return a

    def loss(self, batch, gamma):
        s1, a, o, r, s2 = batch
        s1_vals = self.net(s1).reshape(-1, 4, 4)
        i_a = torch.argmax(a, dim=1)
        i_o = torch.argmax(o, dim=1)
        i_x = torch.arange(i_a.shape[0])
        s1_vals_compare = s1_vals[i_x, i_a, i_o]
        s2_vals = self.target(s2).reshape(-1, 4, 4)
        v1, _ = torch.min(s2_vals, dim=2)
        s2_vals_compare, _ = torch.max(v1, dim=1)
        s2_vals_compare = s2_vals_compare.detach()
        return nn.MSELoss()(s1_vals_compare, s2_vals_compare * gamma + r)

    def train(self, data, optimizer):
        self.n_train = self.n_train + 1
        optimizer.zero_grad()
        loss = self.loss(data, 0.99)
        loss.backward()
        optimizer.step()
        self.losses.append(loss.item())

    def update_target(self):
        self.target.load_state_dict(self.net.state_dict())

    def graph_loss(self, ax, n_mean=10):
        l_t = np.array(self.losses)
        i_trunc = l_t.shape[0] // n_mean * n_mean
        l_trunc = l_t[:i_trunc]
        l_avg = l_trunc.reshape((-1, n_mean)).mean(axis=1)
        ax.plot(l_avg)
        return self

    def save_model(self):
        torch.save(self.target.state_dict(), 'model/state_dict.pth')
        with open('model/n_train.txt', 'w') as f:
            f.write(str(self.n_train))
        with open('model/losses.txt', 'w') as f2:
            for loss in self.losses:
                f2.write(str(round(loss, 4)))
                f2.write(',')

    def load_model(self):
        state_dict = torch.load('model/state_dict.pth', weights_only=True)
        with open("model/n_train.txt", "r") as f:
            self.n_train = int(f.read())
        with open('model/losses.txt', 'r') as f2:
            string_list = f2.read().split(",")[:-1]
            float_list = [float(x) for x in string_list]
        self.losses = float_list
        self.net.load_state_dict(state_dict)
        self.target.load_state_dict(state_dict)

    def parameters(self):
        return self.net.parameters()


class Net(nn.Module):

    def __init__(self, input_size, hidden1, hidden2, output_size):
        super().__init__()
        self.l1 = nn.Linear(input_size, hidden1)
        self.l2 = nn.Linear(hidden1, hidden2)
        self.l3 = nn.Linear(hidden2, output_size)
        self.act = nn.ReLU()

    def forward(self, x):
        x2 = self.act(self.l1(x))
        x3 = self.act(self.l2(x2))
        x4 = self.l3(x3)
        return x4
